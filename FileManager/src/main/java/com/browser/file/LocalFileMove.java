//package com.browser.file;
//
//import com.merlin.api.Reply;
//import com.merlin.api.What;
//import com.merlin.bean.Path;
//import com.merlin.task.file.Cover;
//
//import java.io.File;
//
//public final class FileMove extends FileAction {
//
//    public Reply<Path> move(File from, File toFolder, String name,int coverMode,Progress progress){
//        final String fromName=null!=from?from.getName():null;
//        final Path path=null!=from?Path.build(from):null;
//        if (null==fromName||fromName.length()<=0||null==toFolder||null==name||name.length()<=0){
//            return new Reply(true, What.WHAT_INVALID,"File invalid",path);
//        }else if (!from.exists()){
//            return new Reply(true,What.WHAT_NOT_EXIST,"File not exist",path);
//        }else if (!from.canWrite()){
//            return new Reply(true,What.WHAT_NONE_PERMISSION,"File none read permission",path);
//        }else if (toFolder.exists()&&!toFolder.isDirectory()){//Copy file
//            return new Reply(true,What.WHAT_NOT_DIRECTORY,"Target dir not folder",path);
//        }
//        final File toFile=new File(toFolder,name);
//        if (toFile.exists()){
//            if (coverMode!= Cover.COVER_REPLACE) {
//                return new Reply(true, What.WHAT_EXIST, "File already exist", Path.build(toFile));
//            }
//            File temp=null;
//            while ((temp=new File(toFolder,"."+name+"_"+(Math.random()*10000)+".temp")).exists()){
//                //Do nothing
//            }
//            toFile.renameTo(temp);//Move exist to temp
//            if (toFile.exists()){
//                return new Reply(true, What.WHAT_FAIL, "Fail delete already exist", null);
//            }
//           Reply<Path> reply=move(from,toFolder,name,coverMode,progress);
//           FileDelete fileDelete=new FileDelete();
//           if (null==reply||reply.getWhat()!=What.WHAT_SUCCEED){//Copy fail,rollback
//              fileDelete.deleteFile(toFile,progress);
//              if (!toFile.exists()){
//                  temp.renameTo(toFile);//Rollback
//              }
//           }else{//Copy succeed, delete temp
//               fileDelete.deleteFile(temp,progress);//Delete temp
//           }
//           return reply;
//        }
//        Path copyFrom=Path.build(from);
//        Path copyTo=Path.build(toFile);
//        notify("Moving ",copyFrom,copyTo,0f,progress);
//        from.renameTo(toFile);
//       return toFile.exists()&&!from.exists()?new Reply(true,What.WHAT_SUCCEED,"Succeed move path",copyFrom):
//               new Reply<>(true,What.WHAT_FAIL,"Fail move path",copyFrom);
//    }
//
//}
