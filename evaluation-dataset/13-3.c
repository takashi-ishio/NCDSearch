fix_opids(((Result *) plan)->resconstantqual); 
plan->subPlan = nconc(plan->subPlan,   pull_subplans(((Result *) plan)->resconstantqual)); 
