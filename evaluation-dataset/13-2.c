fix_opids((Node *) ((IndexScan *) plan)->indxqualorig); 
plan->subPlan = nconc(plan->subPlan, pull_subplans((Node *) ((IndexScan *) plan)->indxqual)); 
