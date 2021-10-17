if(!pid){ 
  close(fd[1]); 
  dup2(fd[0], 0); 
  close(fd[0]); 
