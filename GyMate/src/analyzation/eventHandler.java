package analyzation;

import utils.exerciseUtils.eExerciseStatus;
import android.text.format.Time;

public class eventHandler {
	
	private Integer _numberOfReturns;
	private Integer _numberToReach;
	private Time _timeout;
	private Integer _currentSet;
	private eExerciseStatus result;
	
	public eventHandler(Integer iNumberOfReturns, Integer iNumberToReach) {
		_numberOfReturns = iNumberOfReturns;
		_numberToReach = iNumberToReach;
		
		}
	
	
	
	public eExerciseStatus isExerciseFinished()
	{
		eExerciseStatus result = eExerciseStatus.GOAL_PASSED; 
		if(_numberOfReturns.equals(_numberToReach) /*&& _timeout.toMillis(true)*/)
		{
			
		}
		
		return result;
	}

}
