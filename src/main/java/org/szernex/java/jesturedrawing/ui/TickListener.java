package org.szernex.java.jesturedrawing.ui;

public interface TickListener {
	void onTickStart(Ticker ticker);

	void onTickEnd(Ticker ticker);

	void onBreakStart(Ticker ticker);

	void onBreakEnd(Ticker ticker);

	void onNewImage(Ticker ticker);

	void onFinished(Ticker ticker);
}
