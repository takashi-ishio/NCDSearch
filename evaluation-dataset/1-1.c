sprintf(q, "CREATE %s INDEX %s on %s using %s (", 
  (strcmp(indinfo[i].indisunique, "t") == 0) ? "UNIQUE" : "", 
  fmtId(indinfo[i].indexrelname), fmtId(indinfo[i].indrelname),  
  indinfo[i].indamname); 
