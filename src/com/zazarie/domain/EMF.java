package com.zazarie.domain;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class EMF {
	private static final EntityManagerFactory emfInstance = Persistence
			.createEntityManagerFactory("jpa-cloudsql-tutorial");

	private EMF() {
	}

	public static EntityManagerFactory get() {
		return emfInstance;
	}
}
