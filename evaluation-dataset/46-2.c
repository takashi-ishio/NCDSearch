 if (res > 0) {
   for (j=0; j < res; j++) 
     page_cache_release(pages[j]); 
 } 