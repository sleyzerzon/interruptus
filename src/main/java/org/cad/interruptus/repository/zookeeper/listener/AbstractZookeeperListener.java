package org.cad.interruptus.repository.zookeeper.listener;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cad.interruptus.entity.Entity;
import org.cad.interruptus.core.esper.EsperConfiguration;
import org.cad.interruptus.entity.RunnableEntity;

abstract public class AbstractZookeeperListener<E extends Entity> implements EntityConfigurationListener<E>
{
    final EsperConfiguration<E> configuration;
    final Log logger = LogFactory.getLog(getClass());
    final AtomicBoolean isLeader;

    public AbstractZookeeperListener(final EsperConfiguration<E> configuration, final AtomicBoolean isLeader)
    {
        this.configuration = configuration;
        this.isLeader      = isLeader;
    }

    protected void startIfRunning(final RunnableEntity e)
    {
        if ( ! e.isRunning()) {
            return;
        }

        if (e.isMasterOnly() && ! isLeader.get()) {
            return;
        }

        logger.debug(String.format("Starting %s %s", e.getClass().getSimpleName(), e.getId()));
        configuration.start(e.getId());
    }

    protected void stopIfNotRunning(final RunnableEntity e)
    {
        if (e.isRunning()) {
            return;
        }

        logger.debug(String.format("Stopping %s %s", e.getClass().getSimpleName(), e.getId()));
        configuration.stop(e.getId());
    }

    @Override
    public void onSave(final E e)
    {
        configuration.save(e);

        if (e instanceof RunnableEntity) {
            startIfRunning((RunnableEntity) e);
            stopIfNotRunning((RunnableEntity) e);
        }
    }

    @Override
    public void onDelete(final E e)
    {
        configuration.remove(e.getId());
    }
}