fix_opids((Node *) plan->qual); 
plan->subPlan =nconc(plan->subPlan,   pull_subplans((Node *) plan->targetlist)); 