 if (non_present_entry_flush) { 
   if (!cap_caching_mode(iommu->cap)) 
     return 1; 
   else 
     did = 0; 
   } 