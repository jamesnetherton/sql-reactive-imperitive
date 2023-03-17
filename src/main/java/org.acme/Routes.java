package org.acme;

import io.agroal.api.AgroalDataSource;
import io.vertx.mutiny.pgclient.PgPool;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@ApplicationScoped
public class Routes extends RouteBuilder {

    @Inject
    AgroalDataSource dataSource;

    @Inject
    PgPool client;

    @PostConstruct
    void init() {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS messages");
                statement.execute("CREATE TABLE messages (message varchar(50))");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configure() throws Exception {
        // Insert a new message with Camel SQL and the JDBC datasource
        from("timer:tick?period=5s")
                .to("sql:INSERT INTO messages VALUES (:#${exchangeProperty.CamelTimerCounter})");


        // Retrieve message records with the reactive client
        from("timer:tick?period=6s")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("\n\n========================");
                        client.query("SELECT message FROM messages").execute()
                                .subscribe()
                                .asCompletionStage()
                                .get()
                                .forEach(row -> System.out.println("Got message: " + row.getString("message")));
                        System.out.println("========================");
                    }
                });
    }
}
