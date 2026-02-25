package select.parallelism;

import select.SelectionResult;

import java.util.Arrays;
import java.util.BitSet;

public class ParBucketSelect {

    public static SelectionResult select(int[] array, int k) {
        long currentMin = Arrays.stream(array).parallel().min().orElse(Integer.MAX_VALUE);
        long currentMax = Arrays.stream(array).parallel().max().orElse(Integer.MIN_VALUE);

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
            final long finalCurrentMin = currentMin;
            final long finalCurrentMax = currentMax;

            BitSet[] buckets = Arrays.stream(array)
                    .parallel()
                    .filter(n -> n >= finalCurrentMin && n <= finalCurrentMax)
                    .collect(
                            () -> {
                                BitSet[] localBuckets = new BitSet[numBuckets];
                                for (int i = 0; i < numBuckets; i++) {
                                    localBuckets[i] = new BitSet();
                                }
                                return localBuckets;
                            },
                            (localBuckets, n) -> {
                                int idx = (int) ((n - finalCurrentMin) / gap);
                                int offset = (int) ((n - finalCurrentMin) % gap);
                                localBuckets[idx].set(offset);
                            },
                            (buckets1, buckets2) -> {
                                for (int i = 0; i < numBuckets; i++) {
                                    buckets1[i].or(buckets2[i]);
                                }
                            }
                    );

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
            currentMax = finalCurrentMin + (targetBucket + 1) * gap - 1;
            currentMin = finalCurrentMin + targetBucket * gap;
        }

        return new SelectionResult((int) currentMin, totalUniqueCount);
    }
}
