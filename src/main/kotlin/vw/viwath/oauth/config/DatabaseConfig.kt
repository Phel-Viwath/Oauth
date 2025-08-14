package vw.viwath.oauth.config

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class DatabaseConfig(
    @Value("\${spring.datasource.url}") private val url: String,
    @Value("\${spring.datasource.username}") private val username: String,
    @Value("\${spring.datasource.password}") private val password: String
) : AbstractR2dbcConfiguration(){

    @Bean
    override fun connectionFactory(): ConnectionFactory {

        //jdbc:mysql://localhost:3306/sru_library
        val mySqlUrl = url.removePrefix("jdbc:mysql://")
        // mySqlUrl will equal to localhost:3306/sru_library
        val (dbHost, dbPortAndDbName) = mySqlUrl.split(":") // we spit it at :
        // so dbHost will be equal localhost and dbPortAndDbName will equal to  3306/sru_library
        val (dbPort, dbName) = dbPortAndDbName.split("/") // we spit it at /

        return MySqlConnectionFactory.from(
            MySqlConnectionConfiguration.builder()
                .host(dbHost)
                .port(dbPort.toInt())
                .database(dbName)
                .username(username)
                .password(password)
                .build()
        )
    }

    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }


}