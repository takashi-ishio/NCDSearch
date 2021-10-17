 else if (nmatch == nbestMatch){ 
   last_candidate->next = current_candidate; 
   last_candidate = current_candidate; 
   ncandidates++;
 } else  last_candidate->next = NULL; 
 