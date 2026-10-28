package com.parcel.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Connects to the database and migrates the schema when the web application starts. */
@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            Database.init();
        } catch (RuntimeException e) {
            // Keep the application up: /health reports 503 and every request retries the
            // connection, so the portal recovers by itself once the database is available.
            LOG.log(Level.SEVERE, "Database initialisation failed; /health will report 503", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Database.close();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == loader) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "Could not deregister JDBC driver", e);
                }
            }
        }
    }
}
