 if (pfn_valid(pte_pfn(pte)) && 
    pte_page(pte) == ZERO_PAGE(old_addr)) 
    pte = pte_wrprotect(mk_pte(ZERO_PAGE(new_addr), new_vma->vm_page_prot)); 
