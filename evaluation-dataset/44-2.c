  for (nreq = 0; wr; ++nreq, wr = wr->next) { 
    if (unlikely(nreq == MTHCA_TAVOR_MAX_WQES_PER_RECV_DB)) { 
       nreq = 0; 
       doorbell[0] = cpu_to_be32(first_ind << srq->wqe_shift); 
       doorbell[1] = cpu_to_be32(srq->srqn << 8); 
       wmb(); 
       mthca_write64(doorbell, dev->kar + MTHCA_RECEIVE_DOORBELL, MTHCA_GET_DOORBELL_LOCK(&dev->doorbell_lock)); 
