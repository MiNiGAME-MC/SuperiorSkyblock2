package com.bgsoftware.superiorskyblock.database.sql.session.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.database.sql.session.RemoteSQLSession;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class MySQLSession extends RemoteSQLSession {

    public MySQLSession(SuperiorSkyblockPlugin plugin, boolean logging) {
        super(plugin, logging);
    }

    @Override
    public boolean createConnection() {
        log("Trying to connect to remote database (MySQL)...");

        try {
            HikariConfig config = new HikariConfig();
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("SuperiorSkyblock Pool");

            config.setDriverClassName("com.mysql.jdbc.Driver");

            String address = plugin.getSettings().getDatabase().getAddress();
            String dbName = plugin.getSettings().getDatabase().getDBName();
            String userName = plugin.getSettings().getDatabase().getUsername();
            String password = plugin.getSettings().getDatabase().getPassword();
            int port = plugin.getSettings().getDatabase().getPort();

            boolean useSSL = plugin.getSettings().getDatabase().hasSSL();
            boolean publicKeyRetrieval = plugin.getSettings().getDatabase().hasPublicKeyRetrieval();

            config.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + dbName + "?useSSL=" + useSSL);
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&allowPublicKeyRetrieval=%b",
                    address, port, dbName, useSSL, publicKeyRetrieval));
            config.setUsername(userName);
            config.setPassword(password);
            config.setMinimumIdle(5);
            config.setMaximumPoolSize(50);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(plugin.getSettings().getDatabase().getWaitTimeout());
            config.setMaxLifetime(plugin.getSettings().getDatabase().getMaxLifetime());
            config.addDataSourceProperty("characterEncoding", "utf8");
            config.addDataSourceProperty("useUnicode", "true");

            dataSource = new HikariDataSource(config);

            log("Successfully established connection with remote database!");

            ready.complete(null);

            return true;
        } catch (Throwable error) {
            log("&cFailed to connect to the remote database:");
            error.printStackTrace();
            PluginDebugger.debug(error);
        }

        return false;
    }

}
