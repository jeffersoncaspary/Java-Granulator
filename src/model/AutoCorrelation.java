package model;
/***
 * The goal of this class is to use autocorrelation to determine
 * how noisy a sample/ granule is
 * @author jgort
 *
 */
public class AutoCorrelation {
    /**
     * 
     * @param lag - number of data points to lag by
     * @return
     */
	public static double NormalizedAutoCorrelation(double[][] sample, int offset, int granuleDuration, int lag) {
		double normalizationFactor = 0;
		double sumOfSquaresNoLag = 0;
		double sumOfSquaresLag = 0;
		double correlation = 0;
		for (int n = 0; n < granuleDuration - lag; n ++) {
			double sample1 = stereoToMono(sample, n + offset);
			double samplePlusLag = stereoToMono(sample, n + offset + lag);
			sumOfSquaresNoLag += Math.pow(sample1, 2);
			sumOfSquaresLag += Math.pow(samplePlusLag, 2);
			correlation += sample1*samplePlusLag;
		}
		correlation /= granuleDuration -lag;
		normalizationFactor = Math.pow(sumOfSquaresNoLag * sumOfSquaresLag, .5) / (granuleDuration - lag);
		if (normalizationFactor == 0) {
			return 0;
		}
		return correlation /normalizationFactor;
	}

	private static double stereoToMono(double[][] sample, int i) {
		if (sample.length == 1) {
			return sample[0][i];
		}
		double mono = 0;
		for (int j = 0; j < sample.length; j ++) {
			mono += sample[j][i];
		}
		return mono /sample.length;
	}
}
