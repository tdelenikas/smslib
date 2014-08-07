
package org.smslib.core;

public class Coverage
{
	String msisdn;

	boolean coverage;

	double creditsUsed;

	public Coverage(String msisdn)
	{
		this.msisdn = msisdn;
		this.coverage = false;
		this.creditsUsed = 0;
	}

	public String getMsisdn()
	{
		return this.msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	public boolean getCoverage()
	{
		return this.coverage;
	}

	public void setCoverage(boolean coverage)
	{
		this.coverage = coverage;
	}

	public double getCreditsUsed()
	{
		return this.creditsUsed;
	}

	public void setCreditsUsed(double creditsUsed)
	{
		this.creditsUsed = creditsUsed;
	}

	@Override
	public String toString()
	{
		StringBuffer b = new StringBuffer(1024);
		b.append(String.format("MSISDN: %s%n", getMsisdn()));
		b.append(String.format("Is destination covered?: %b%n", getCoverage()));
		b.append(String.format("Credits used: %f%n", getCreditsUsed()));
		return b.toString();
	}
}
