package processing.analyzing;

public class ReuseProbValue {

	private double sum;
	private double count;
	
	public ReuseProbValue() {
		this.sum = 0.0;
		this.count = 0.0;
	}
	
	public void increment(double value) {
		this.sum += value;
		this.count++;
	}
	
	public double getCount() {
		return this.count;
	}
	
	public double getSum() {
		return this.sum;
	}
}
