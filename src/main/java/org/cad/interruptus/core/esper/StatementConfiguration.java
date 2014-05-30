package org.cad.interruptus.core.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPStatementException;
import com.espertech.esper.client.EPStatementState;
import java.util.ArrayList;
import java.util.List;
import org.cad.interruptus.entity.Statement;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class StatementConfiguration implements EsperConfiguration<String, Statement>
{
    final private static Log log = LogFactory.getLog(StatementConfiguration.class);
    private final EPAdministrator epAdministrator;

    public StatementConfiguration(final EPAdministrator epAdministrator)
    {
        this.epAdministrator = epAdministrator;
    }

    @Override
    public List<Statement> list()
    {
        String[] statementNames    = epAdministrator.getStatementNames();
        List<Statement> statements = new ArrayList<>();

        for (String name : statementNames) {
            EPStatement epStatement = epAdministrator.getStatement(name);
            statements.add(new Statement(name, epStatement.getText(), false));
        }

        return statements;
    }

    @Override
    public void save(final Statement statement) throws EPStatementException
    {
        final EPStatement sttm = epAdministrator.createEPL(statement.getQuery(), statement.getName());

        if ( ! statement.isRunning() && ! sttm.isStopped()) {
            sttm.stop();
        }

        if ( ! statement.getDebug()) {
            return;
        }

        sttm.addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents)
            {
                for (EventBean newEvent : newEvents) {
                    log.info(newEvent.getUnderlying());
                }
            }
        });
    }

    public Boolean start(final Statement statement)
    {
        return start(statement.getName());
    }

    @Override
    public Boolean start(final String name)
    {
        try {
            final EPStatement epStatement = epAdministrator.getStatement(name);

            if (epStatement != null) {
                epStatement.start();
            }

            return true;
        } catch (EPStatementException e) {
            log.info(e.getMessage());
            return false;
        }
    }

    public Boolean startAll()
    {
        try {
            epAdministrator.startAllStatements();
            return true;
        } catch (EPException e) {
            log.info(e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean stop(final String name)
    {
        try {
            final EPStatement epStatement = epAdministrator.getStatement(name);

            if (epStatement == null) {
                return false;
            }

            if (epStatement.isStopped()) {
                return true;
            }

            epStatement.stop();

            return true;
        } catch (EPStatementException e) {
            log.info(e.getMessage());
            return false;
        }
    }

    public Boolean stop(final Statement statement)
    {
        return stop(statement.getName());
    }

    public Boolean stopAll()
    {
        try {
            epAdministrator.stopAllStatements();
            return true;
        } catch (EPException e) {
            log.info(e.getMessage());
            return false;
        }
    }

    public Boolean destroyAll() throws EPException
    {
        try {
            epAdministrator.destroyAllStatements();
            return true;
        } catch (EPException e) {
            log.info(e.getMessage());
            return false;
        }
    }

    public EPStatementState getStatementState(final String statementName) throws EPStatementException
    {
        try {
            final EPStatement epStatement = epAdministrator.getStatement(statementName);

            if (epStatement != null) {
                return epStatement.getState();
            }

        } catch (EPStatementException e) {
            log.info(e.getMessage());
        }

        log.info("Statement: " + statementName + " not found");

        return null;
    }

    @Override
    public Boolean remove(final String name)
    {
        try {
            final EPStatement epStatement = epAdministrator.getStatement(name);

            if (epStatement != null) {
                epStatement.destroy();
            }

            return true;

        } catch (EPStatementException e) {
            log.info(e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean remove(final Statement e)
    {
        return this.remove(e.getName());
    }

    @Override
    public Boolean exists(final String name)
    {
        return epAdministrator.getStatement(name) != null;
    }
}