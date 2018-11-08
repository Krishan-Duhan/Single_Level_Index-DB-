/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexing_krishan;

import java.io.*;
import java.util.*;

/**
 *
 * @author Krishan Kumar
 */
public class Indexing_Krishan {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException{
        RandomAccessFile raf_in = new RandomAccessFile(args[1], "r");  //Data file
                
        if(args.length == 0){
            System.out.println("No arguments passed.");
        }
        
        //Create an arraylist of strings to store the key and pointer of data record in memory
        ArrayList<String> arrlist = new ArrayList<String>();
        long offset = 0;
        String key_str = args[3];
        int key_len = Integer.parseInt(key_str);   //converted the key length to integer
        
        //Check the key length
        if(key_len > 24 || key_len < 1){
            System.out.println("Invalid key length passed");
            return;
        }
        
        try{
        if(args[0].equals("-c")){
            System.out.println("Creating index file");
            File file = new File(args[2]);
            file.delete();                // delete the old index file if it already exists
            RandomAccessFile raf_op = new RandomAccessFile(args[2], "rw"); //Index file
            String line, temp, offset_str;
            
            //Form an arraylist of strings where each string to contain key and offset for each record
            while((line = raf_in.readLine()) != null){
                offset_str = Long.toString(offset);
                //temp = line.substring(0, (key_len - 1)) + "[" + offset_str + "]";
                temp = line.substring(0, key_len) + ":" + offset_str;
                arrlist.add(temp);                 //adding string to the arraylist
                offset = raf_in.getFilePointer();
            }
            
            //Now sort the arraylist
            Collections.sort(arrlist);

            temp = "";
            String temp_key;
            long temp_offset ;
            
            //Now we add the key and the pointer of data record from the arraylist to the output index file
            for(int i = 0; i < arrlist.size(); i++){
                String[] temp_str = arrlist.get(i).split(":");
                temp_key = temp_str[0];
                temp_offset = Long.parseLong(temp_str[1]);
                //System.out.println("temp_key:" + temp_key);
                //System.out.println("temp_offset:" + temp_offset);
                raf_op.writeBytes(temp_key);
                raf_op.writeLong(temp_offset);
                raf_op.writeBytes("\r\n");
                //raf_op.writeChar(10);            
            }
            //closing the data file and the index file
            raf_op.close();
            raf_in.close();
            File file1 = new File(args[2]);   // We will use it to change file permission to read-only
            file1.setWritable(false);
        }
        else if(args[0].equals("-l")){
            System.out.println("Listing data file using index file:");
            RandomAccessFile raf_op = new RandomAccessFile(args[2], "r"); //Index file in read-only mode
            long offset_read = 0;
            long offset_address;
            String line, line_display;
            
            do{
                //First, get the data pointer from index file
                offset_address = raf_op.getFilePointer() + key_len;   //set the file pointer to beginning of eight-byte data pointer in each record of index file
                raf_op.seek(offset_address);
                offset_read = raf_op.readLong();        //read the 8-byte data pointer from each record in index file
                //System.out.println("offset_read from readlong: " + offset_read);
                
                //Now, display data from the data file using the pointer found above
                raf_in.seek(offset_read);         //set file pointer to the position where we have to read data from this data file.
                line_display = raf_in.readLine();
                System.out.println(line_display);
            }while((line = raf_op.readLine()) != null);
            
            //closing the data file and the index file
            raf_op.close();
            raf_in.close();
        }
        } catch(EOFException exception){
            System.out.println("EOF reached");
        }
    }
    
}
