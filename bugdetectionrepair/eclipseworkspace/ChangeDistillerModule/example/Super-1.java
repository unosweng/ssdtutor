package pullUpMethod.after;

import java.util.ArrayList;
import java.util.List;

public class Super {
	List<String> names = new ArrayList<String>();

	String m1(int i) {
		
		for(int m=0; m<i; m++){
			System.out.println(m);
		}
		
		return names.get(i);
	}
}

