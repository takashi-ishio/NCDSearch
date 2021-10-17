  INIT_RADIX_TREE(&mapping->page_tree, GFP_ATOMIC); 
  spin_lock_init(&mapping->tree_lock); 
  INIT_LIST_HEAD(&mapping->private_list); 
  spin_lock_init(&mapping->private_lock); 
  spin_lock_init(&mapping->i_mmap_lock); 
  INIT_RAW_PRIO_TREE_ROOT(&mapping->i_mmap); 
  INIT_LIST_HEAD(&mapping->i_mmap_nonlinear); 
