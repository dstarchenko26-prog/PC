package select.sequence;

import select.SelectionResult;

import java.util.BitSet;

public class BucketSelect {

    public static SelectionResult select(int[] array, int k) {
        long currentMin = Integer.MAX_VALUE;
        long currentMax = Integer.MIN_VALUE;

        for (int n : array) {
            if (n < currentMin) currentMin = n;
            if (n > currentMax) currentMax = n;
        }

        if (currentMin == currentMax) {
            return new SelectionResult((int) currentMin, 1);
        }

        int targetK = k;
        int numBuckets = 100;

        int totalUniqueCount = 0;
        boolean isFirstPass = true;

        while (currentMin < currentMax) {
            long range = currentMax - currentMin;
            long gap = range / numBuckets + 1;

            BitSet[] buckets = new BitSet[numBuckets];
            for (int i = 0; i < numBuckets; i++) {
                buckets[i] = new BitSet();
            }

            for (int n : array) {
                if (n >= currentMin && n <= currentMax) {
                    int idx = (int) ((n - currentMin) / gap);
                    int offset = (int) ((n - currentMin) % gap);
                    buckets[idx].set(offset);
                }
            }

            if (isFirstPass) {
                for (int i = 0; i < numBuckets; i++) {
                    totalUniqueCount += buckets[i].cardinality();
                }
                isFirstPass = false;

                if (k > totalUniqueCount) {
                    throw new IllegalArgumentException("У масиві лише " + totalUniqueCount + " унікальних елементів. k=" + k + " знайти неможливо.");
                }
            }

            int sum = 0;
            int targetBucket = 0;

            for (int i = numBuckets - 1; i >= 0; i--) {
                int uniqueCount = buckets[i].cardinality();
                if (sum + uniqueCount >= targetK) {
                    targetBucket = i;
                    break;
                }
                sum += uniqueCount;
            }

            targetK -= sum;
            currentMax = currentMin + (targetBucket + 1) * gap - 1;
            currentMin = currentMin + targetBucket * gap;
        }

        return new SelectionResult((int) currentMin, totalUniqueCount);
    }
}
