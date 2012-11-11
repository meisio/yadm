package common;

import java.sql.Timestamp;

import org.joda.time.DateTime;

import com.google.common.base.Optional;

/**
 * This class provides some common method for time.
 * 
 * @author Eugen Meissner
 *
 */
public class TimeUtils {

	/**
	 * This method returns the string based expires value in {@link Timestamp}.
	 * It also adds the expires value to current system time.
	 * 
	 * @return {@link Timestamp}
	 */
	public static Optional<Timestamp> getTimeStamp(String expires){
		if(expires != null){
			if(expires.equals("unlimited")){
				return Optional.of(new Timestamp(0));
			}
			
			DateTime time = DateTime.now();
			if(expires.indexOf('h')==expires.length()-1){
				int h = getNumberedTimestamp(expires);
				return Optional.of(new Timestamp(time.plusHours(h).getMillis()));
			} else if(expires.indexOf('d')==expires.length()-1){
				int d = getNumberedTimestamp(expires);
				return Optional.of(new Timestamp(time.plusDays(d).getMillis()));
			}
			
		}
		
		return Optional.absent();
	}
	
	/**
	 * This method extracts the numbered value of the string based expires value.
	 * 
	 * @return numbered time.
	 */
	private static int getNumberedTimestamp(String expires){
		String s = new String(expires);
		return Integer.valueOf(s.substring(0, s.length()-1));
	}
	
}
