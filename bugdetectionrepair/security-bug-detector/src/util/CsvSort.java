package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import input.IGlobalProperty;

public class CsvSort {

   public static List<List<String>> sortTreeList(List<String> treeList) {
      List<List<String>> lines = new ArrayList<List<String>>();
      for (String line : treeList) {
         List<String> list = Arrays.asList(line.split(IGlobalProperty.COLUMN_SEPARATOR));
         lines.add(list);
      }

      Comparator<List<String>> c0 = createAscendingComparator(0);
      Comparator<List<String>> c1 = createAscendingComparator(1);
      Comparator<List<String>> c2 = createAscendingComparator(2);
      Comparator<List<String>> c3 = createDesendingComparator(3);
      Comparator<List<String>> c4 = createAscendingComparator(4);
      Comparator<List<String>> comparator = createComparator(c0, c1, c2, c3, c4);
      Collections.sort(lines, comparator);

      return lines;
   }

   public static List<List<String>> sortIndicatorList(List<String> treeList) {
      List<List<String>> lines = new ArrayList<List<String>>();
      for (String line : treeList) {
         List<String> list = Arrays.asList(line.split(IGlobalProperty.COLUMN_SEPARATOR));
         lines.add(list);
      }

      Comparator<List<String>> c0 = createAscendingComparator(0);
      Comparator<List<String>> c1 = createAscendingComparator(1);
      Comparator<List<String>> c2 = createAscendingComparator(2);
      Comparator<List<String>> c3 = createAscendingComparator(3);
      Comparator<List<String>> c4 = createAscendingComparator(4);
      Comparator<List<String>> comparator = createComparator(c0, c1, c2, c3, c4);
      Collections.sort(lines, comparator);

      return lines;
   }

   @SafeVarargs
   private static <T> Comparator<T> createComparator(Comparator<? super T>... delegates) {
      return (t0, t1) -> {
         for (Comparator<? super T> delegate : delegates) {
            int n = delegate.compare(t0, t1);
            if (n != 0) {
               return n;
            }
         }
         return 0;
      };
   }

   private static <T extends Comparable<? super T>> Comparator<List<T>> createAscendingComparator(int index) {
      return createListAtIndexComparator(Comparator.naturalOrder(), index);
   }

   private static <T extends Comparable<? super T>> Comparator<List<T>> createDesendingComparator(int index) {
      return createListAtIndexComparator(Comparator.reverseOrder(), index);
   }

   private static <T> Comparator<List<T>> createListAtIndexComparator(Comparator<? super T> delegate, int index) {
      return (list0, list1) -> delegate.compare(list0.get(index), list1.get(index));
   }

}