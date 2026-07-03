package com.jacksam.productfilter.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "filter_rules")
public class FilterRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String field;

    @Column(nullable = false)
    private String operator;

    @Column(name = "rule_value", nullable = false)
    private String ruleValue;

    @Column(name = "logic_group")
    private String logicGroup = "default";

    @Column(name = "rule_order")
    private int ruleOrder;

    @Column(name = "action_type")
    private String actionType = "TAG";

    @Column(name = "action_value")
    private String actionValue;

    private boolean enabled = true;

    public FilterRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRuleValue() { return ruleValue; }
    public void setRuleValue(String ruleValue) { this.ruleValue = ruleValue; }
    public String getLogicGroup() { return logicGroup; }
    public void setLogicGroup(String logicGroup) { this.logicGroup = logicGroup; }
    public int getRuleOrder() { return ruleOrder; }
    public void setRuleOrder(int ruleOrder) { this.ruleOrder = ruleOrder; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getActionValue() { return actionValue; }
    public void setActionValue(String actionValue) { this.actionValue = actionValue; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
