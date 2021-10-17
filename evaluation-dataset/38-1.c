  rdp = &__get_cpu_var(rcu_data); 
 *rdp->nxttail = head; 
  rdp->nxttail = &head->next;
  if (unlikely(++rdp->qlen > qhimark)) { 
    rdp->blimit = INT_MAX; 
    force_quiescent_state(rdp, &rcu_ctrlblk);       
