package com.seasun.data.simple_report.collect;

import java.time.LocalDateTime;

public interface EventHandle {

	public void poorSignalEvent(LocalDateTime time, int sig);
	
	public void esenseEvent(LocalDateTime time, int attentionLevel, int meditationLevel);

	public void blinkEvent(LocalDateTime time, int blinkStrength);

	public void eegPowerEvent(LocalDateTime time, int delta, int theta, int low_alpha, int high_alpha, int low_beta, int high_beta,
			int low_gamma, int mid_gamma);

	public void rawEegEvent(LocalDateTime time, int raw, int index);

}
