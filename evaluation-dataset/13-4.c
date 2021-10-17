fix_opids((Node *) ((HashJoin *) plan)->hashclauses);   
plan->subPlan =nconc(plan->subPlan,   pull_subplans((Node *) ((HashJoin *) plan)->hashclauses)); 
