
#include "FileOperator.h"

int fileOpen(const char *filename, int access){
	int fd=0;
	if(access == _CREATE){
		fd = open(filename,O_RDWR|O_CREAT|O_EXCL,0666);
	}else if(access == _WRONLY){
		fd = open(filename,O_WRONLY|O_TRUNC);
	}else if(access == _RDONLY){
		fd = open(filename,O_RDONLY,0666);
	}else if(access == _RDWR){
		fd = open(filename,O_RDWR);
	}else if (access==_ACRDRW){
		fd=open(filename,O_RDWR|O_CREAT,0666);
	}
	return fd;
}

int fileRead(int fd, unsigned char *buf, int size){
	return read(fd, buf, size);  
}  

int fileWrite(int fd, unsigned char *buf, int size){
	return write(fd, buf, size);  
}

int64_t fileSeek(int fd, int64_t pos, int whence){
	if (whence == 0x10000) {
		struct stat st;
		int ret = fstat(fd, &st);
		return ret < 0 ? -1 : st.st_size;
	}
	return lseek(fd, pos, whence);
}

int fileClose(int fd){
	return close(fd);  
}

