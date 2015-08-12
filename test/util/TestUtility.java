package util;

import java.util.Collection;
import java.util.HashSet;

public class TestUtility {

	public static boolean compareCollections(Collection<?> set1, Collection<?> set2){
		if (set1.size() != set2.size())
			return false;
		Collection<?> aux1 = new HashSet<Object>(set1);
		aux1.removeAll(set2);
		Collection<?> aux2 = new HashSet<Object>(set2);
		aux2.removeAll(set1);
		return aux1.isEmpty() && aux2.isEmpty();
	}
}
