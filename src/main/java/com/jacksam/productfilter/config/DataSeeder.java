package com.jacksam.productfilter.config;

import com.jacksam.productfilter.entity.Category;
import com.jacksam.productfilter.entity.Product;
import com.jacksam.productfilter.entity.Role;
import com.jacksam.productfilter.entity.User;
import com.jacksam.productfilter.enums.Permission;
import com.jacksam.productfilter.repository.CategoryRepository;
import com.jacksam.productfilter.repository.ProductRepository;
import com.jacksam.productfilter.repository.RoleRepository;
import com.jacksam.productfilter.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (roleRepository.count() > 0) return;

        Role adminRole = roleRepository.save(
                new Role("ADMIN", "System administrator with full access"));
        adminRole.setPermissions(Set.of(Permission.values()));
        roleRepository.save(adminRole);

        Role managerRole = roleRepository.save(
                new Role("MANAGER", "Department manager"));
        managerRole.setPermissions(Set.of(
                Permission.PRODUCT_VIEW, Permission.PRODUCT_EDIT,
                Permission.PRODUCT_CREATE, Permission.INVENTORY_VIEW,
                Permission.ORDER_VIEW, Permission.ACCESS_GRANT));
        roleRepository.save(managerRole);

        Role viewerRole = roleRepository.save(
                new Role("VIEWER", "Read-only access"));
        viewerRole.setPermissions(Set.of(Permission.PRODUCT_VIEW));
        roleRepository.save(viewerRole);

        User admin = userRepository.save(
                new User("admin", passwordEncoder.encode("admin123"),
                        "admin@jacksam.com", "Admin User"));
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);

        User manager = userRepository.save(
                new User("manager", passwordEncoder.encode("manager123"),
                        "manager@jacksam.com", "Manager User"));
        manager.setRoles(Set.of(managerRole));
        manager.setDepartmentId(1L);
        userRepository.save(manager);

        User viewer = userRepository.save(
                new User("viewer", passwordEncoder.encode("viewer123"),
                        "viewer@jacksam.com", "Viewer User"));
        viewer.setRoles(Set.of(viewerRole));
        viewer.setDepartmentId(1L);
        userRepository.save(viewer);

        Category electronics = categoryRepository.save(
                new Category("Electronics", "Electronic devices and accessories", null));
        Category laptops = categoryRepository.save(
                new Category("Laptops", "Laptop computers", electronics.getId()));
        Category phones = categoryRepository.save(
                new Category("Phones", "Mobile phones", electronics.getId()));
        Category clothing = categoryRepository.save(
                new Category("Clothing", "Apparel and accessories", null));

        seedProduct("MacBook Pro 16", "Apple M3 Pro chip, 18GB RAM, 512GB SSD",
                new BigDecimal("2499.99"), 25, laptops, admin.getId());
        seedProduct("ThinkPad X1 Carbon", "Intel i7, 16GB RAM, 512GB SSD",
                new BigDecimal("1899.99"), 15, laptops, admin.getId());
        seedProduct("iPhone 15 Pro", "256GB, Titanium, A17 Pro chip",
                new BigDecimal("1199.99"), 50, phones, admin.getId());
        seedProduct("Samsung Galaxy S24", "256GB, Snapdragon 8 Gen 3",
                new BigDecimal("999.99"), 40, phones, manager.getId());
        seedProduct("Sony WH-1000XM5", "Wireless Noise Cancelling Headphones",
                new BigDecimal("349.99"), 100, electronics, manager.getId());
        seedProduct("Leather Jacket", "Genuine leather, premium finish",
                new BigDecimal("299.99"), 30, clothing, admin.getId());
        seedProduct("Denim Jeans", "Classic fit, 100% cotton",
                new BigDecimal("79.99"), 200, clothing, manager.getId());
        seedProduct("iPad Pro 12.9", "M2 chip, 256GB, Liquid Retina XDR",
                new BigDecimal("1299.99"), 20, electronics, admin.getId());

        System.out.println("Seed data loaded: "
                + userRepository.count() + " users, "
                + categoryRepository.count() + " categories, "
                + productRepository.count() + " products");
    }

    private void seedProduct(String name, String desc, BigDecimal price,
                             int qty, Category category, Long ownerId) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setQuantity(qty);
        p.setCategory(category);
        p.setOwnerId(ownerId);
        productRepository.save(p);
    }
}
