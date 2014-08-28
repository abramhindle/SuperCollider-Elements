# 
# Copyright 2014 Abram Hindle
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 

elms <- c("Fe","Rb","H","Li","He","Be","B","C","O","N","S","Cl");

sapply(elms, function(elm) {
                                        #v <- read.csv("~/Documents/H.csv",header=F,sep="|")
    v <- read.csv(paste("Documents/",elm,".csv",sep=""),header=F,sep="|")
    v$V1 <- as.numeric(as.character(v$V1))
    v$V2 <- as.numeric(as.character(v$V2))
    v <- v[is.finite(v$V2),]
    v <- v[is.finite(v$V1),]
    
    base = 1 # 20 might be good
                                        # 200 nm
    scaler = base/(1.0/200)#//min(1/v$V1)
    ap <- approxfun(scaler * 1/v$V1,v$V2)
    out <- c()
    
    pitches <- scaler*1/v$V1
    ourord <- order(pitches)
    out$hz <- pitches[ourord]
    out$v  <- v$V2[ourord]
    write.csv(out,paste(elm,".csv",sep=""))
    #npitches <- round(pitches)
    #x <- aggregate(v$V2,by=list(npitches),FUN=sum)
    #z <- c(1:20000)*0
    #zl <- c(1:20000)
    #z[is.na(z)] <- 0.0
    #z[x$Group.1[x$Group.1 <= 20000]] = x$x[x$Group.1 <= 20000]
});
