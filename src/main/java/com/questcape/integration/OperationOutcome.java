package com.questcape.integration;

import lombok.Value;

/** The originating run/session remains attached to an operation's UI outcome. */
@Value
public class OperationOutcome
{
	public enum State
	{
		IDLE, RUNNING, FAILED
	}

	long run;
	long account;
	State state;
	String detail;
}
