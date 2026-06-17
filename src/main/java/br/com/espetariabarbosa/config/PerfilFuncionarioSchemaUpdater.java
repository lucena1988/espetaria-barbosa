package br.com.espetariabarbosa.config;

import br.com.espetariabarbosa.enums.PerfilFuncionario;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PerfilFuncionarioSchemaUpdater implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!isPostgreSql()) {
            return;
        }

        String perfisPermitidos = Arrays.stream(PerfilFuncionario.values())
                .map(perfil -> "'" + perfil.name() + "'")
                .collect(Collectors.joining(", "));

        jdbcTemplate.execute("alter table if exists funcionario drop constraint if exists funcionario_perfil_check");
        jdbcTemplate.execute("alter table if exists funcionario add constraint funcionario_perfil_check check (perfil in (" + perfisPermitidos + "))");
    }

    private boolean isPostgreSql() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        }
    }
}
