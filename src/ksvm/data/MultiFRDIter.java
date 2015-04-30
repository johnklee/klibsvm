package ksvm.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

public class MultiFRDIter implements IRecordIter{
	private BufferedReader 	br = null;
	private List<File> 		fileList;
	private int 			fileIdx = 0;
	private String 			nextProcline = null;
	
	public MultiFRDIter(List<File> fileList) throws Exception
	{
		this.fileList = fileList;
		if(fileList.size()>0)
		{
			System.out.printf("\t[Test] Process %s...\n", fileList.get(fileIdx).getAbsolutePath());
			br = new BufferedReader(new FileReader(fileList.get(fileIdx)));
			retriveProcline();
		}
	}
	
	protected void retriveProcline()
	{
		try
		{
			do
			{
				nextProcline = br.readLine();
				if(nextProcline!=null && 
				   (nextProcline.isEmpty() || nextProcline.startsWith("#"))) continue;
				else 
				{
					if(nextProcline==null && 
					   (fileIdx+1)<fileList.size())
					{
						fileIdx+=1;
						br.close();
						System.out.printf("\t[Test] Process %s...\n", fileList.get(fileIdx).getAbsolutePath());
						br = new BufferedReader(new FileReader(fileList.get(fileIdx)));
						retriveProcline();
					}
					else break;
				}
			}while(nextProcline!=null);
			if(nextProcline==null) br.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			nextProcline = null;
		}
	}

	@Override
	public boolean hasNext() {
		return (nextProcline!=null);
	}

	@Override
	public Record next() {
		if(nextProcline!=null)
		{
			//System.out.printf("\t[Test] line=%s\n", nextProcline);
			StringTokenizer st = new StringTokenizer(nextProcline," \t\n\r\f:");
			Record rd = new Record(st);
			retriveProcline();
			return rd;
		}
		return null;
	}

	@Override
	public void remove() {
		throw new java.lang.UnsupportedOperationException("Not support");			
	}
}
