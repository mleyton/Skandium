/*   Skandium: A Java(TM) based parallel skeleton library.
 *   
 *   Copyright (C) 2009 NIC Labs, Universidad de Chile.
 * 
 *   Skandium is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Skandium is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.

 *   You should have received a copy of the GNU General Public License
 *   along with Skandium.  If not, see <http://www.gnu.org/licenses/>.
 */
package cl.niclabs.skandium.examples.strassen;

public class Matrix{

	int [][]a;
	
	public Matrix(int[][] a){
		int n = a.length;
		this.a= new int[n][n];
		
        for(int i=0; i<n; i++)
        	for(int j=0; j<n; j++)
        		this.a[i][j]=a[i][j];
	}
	
	public Matrix(int n) {
		this.a= new int[n][n];
		
        for(int i=0; i<n; i++)
        	for(int j=0; j<n; j++)
        		this.a[i][j]=0;
	}

	public Matrix add(Matrix m){
        int n = m.length();

        int [][] result = new int[n][n];

        for(int i=0; i< n; i++)
        	for(int j=0; j<n; j++)
        		result[i][j] = a[i][j] + m.a[i][j];

        return new Matrix(result);
	}
	
	
	public Matrix substract(Matrix m){
        int n = m.length();

        int [][] result = new int[n][n];

        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++)
                result[i][j] = a[i][j] - m.a[i][j];

        return new Matrix(result);
	}
	
    public Matrix multiply(Matrix m){
    	int n = m.length();
    	
    	int [][] result = new int[n][n];
    	
    	for (int i = 0; i < n; i++)
		   for (int j = 0; j < n; j++)
		      for (int k = 0; k < n; k++)
		    	  result[i][j] += a[i][k] * m.a[k][j];
    	
    	return new Matrix(result);
	}
	
    public int length(){
    	return a.length;
    }
    
	public Matrix part(int iB, int jB){
		
		int child[][] = new int[a.length/2][a.length/2];
		                          
		for(int i1 = 0, i2=iB; i1<child.length; i1++, i2++)
			for(int j1 = 0, j2=jB; j1<child.length; j1++, j2++)
				child[i1][j1] = a[i2][j2];
		
		return new Matrix(child);
	}
	
    public void copyInto(Matrix child, int iB, int jB) {
        for(int i1 = 0, i2=iB; i1<child.a.length; i1++, i2++)
            for(int j1 = 0, j2=jB; j1<child.a.length; j1++, j2++)
                a[i2][j2] = child.a[i1][j1];
    }

    @Override
	public String toString() {
    	String result="";
    	for (int i=0;i<a.length;i++){
    		for (int j=0;j<a.length;j++)
    			result += a[i][j] + " ";
    		result += "\n";
    	}
    	
    	return result;
    }
}