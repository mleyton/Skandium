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
package cl.niclabs.skandium.examples.pi;

import java.math.BigDecimal;
import java.math.BigInteger;

import cl.niclabs.skandium.muscles.Execute;


/**
 * <p>This class computes Pi decimals for a given interval, using Bailey Borwein Plouffe (BPP) formula, using
 * custom data types (BigDecimal).</p>
 * 
 * <p>Note that the bigger the scale the slower operations are for BigDecimal types.</p>
 * 
 * <p>TODO: This class should be re-implemented without BigDecimal to compute hexadecimal values of Pi's "decimals".</p>
 * 
 * @author mleyton
 *
 */
public class PiComputer implements Execute<Interval, BigDecimal>{
	
    private static final int ROUND_MODE = BigDecimal.ROUND_HALF_EVEN;
    private BigDecimal ZERO = new BigDecimal("0");
    private BigDecimal ONE = new BigDecimal("1");
    private BigDecimal OPPOSITE_ONE = new BigDecimal("-1");
    private BigDecimal OPPOSITE_TWO = new BigDecimal("-2");
    private BigDecimal FOUR = new BigDecimal("4");
    private BigDecimal FIVE = new BigDecimal("5");
    private BigDecimal SIX = new BigDecimal("6");
    private BigDecimal EIGHT = new BigDecimal("8");
    private BigInteger SIXTEEN = new BigInteger("16");
    
	@Override
	public BigDecimal execute(Interval interval) {

		//scale trick to hold enough precision
		int scale = (int) Math.floor(interval.end*1.2);
		BigDecimal bd = ZERO.setScale(scale);
		
    	BigDecimal ONE            = this.ONE.setScale(scale);
    	BigDecimal OPPOSITE_ONE   = this.OPPOSITE_ONE.setScale(scale);
    	BigDecimal OPPOSITE_TWO   = this.OPPOSITE_TWO.setScale(scale);
    	BigDecimal FOUR           = this.FOUR.setScale(scale);
    	BigDecimal FIVE           = this.FIVE.setScale(scale);
    	BigDecimal SIX            = this.SIX.setScale(scale);
    	BigDecimal EIGHT          = this.EIGHT.setScale(scale);

        
        // BBP formula for the given interval
        for (int k = interval.start; k <= interval.end; k++) {
            bd = bd.add(f(k, interval.end, EIGHT, ONE, FOUR, FIVE, SIX, OPPOSITE_TWO, OPPOSITE_ONE));
        }
        
        return bd;
	}

    private BigDecimal f(int k, int scale, 
    		    BigDecimal EIGHT, BigDecimal ONE, BigDecimal FOUR, BigDecimal FIVE, BigDecimal SIX, 
    			BigDecimal OPPOSITE_TWO, BigDecimal OPPOSITE_ONE) {
    	
        BigDecimal K       =  new BigDecimal(k);
        BigDecimal EIGHT_K =  EIGHT.multiply(K);
        BigDecimal FIRST   =  ONE.divide(new BigDecimal(SIXTEEN.pow(k)), ROUND_MODE);
        BigDecimal SECOND  =  FOUR.divide(EIGHT_K.add(ONE), ROUND_MODE);
        BigDecimal THIRD   =  OPPOSITE_TWO.divide(EIGHT_K.add(FOUR), ROUND_MODE);
        BigDecimal FOURTH  =  OPPOSITE_ONE.divide(EIGHT_K.add(FIVE), ROUND_MODE);
        BigDecimal FIFTH   =  OPPOSITE_ONE.divide(EIGHT_K.add(SIX), ROUND_MODE);

        return FIRST.multiply(SECOND.add(THIRD.add(FOURTH.add(FIFTH))));
    }

}
