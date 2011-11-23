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
package cl.niclabs.skandium.examples.nqueensnaive;

public class Count {

	long column[];
	int n;
	
	public Count(int n){
		this.n=n;
		this.column = new long[n];
		
		for(int i=0;i<n;i++){
			column[i]=0;
		}
	}

	public void add(Count count) {
		
		for(int i=0; i<n; i++){
			column[i]+=count.column[i];
		}
	}

	public long total(){
		long total=0;
		
		for(int i=0; i<n; i++){
			total+=this.column[i];
		}
		
		return total;
	}
	
	@Override
	public String toString(){
		
		String text="";
    	for(int i=0;i < this.n; i++){
    		text += " "+this.column[i];
    	}
    
    	return "Total:"+total()+" Columns:"+text.trim();
	}
}