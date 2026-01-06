package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.api.v2.dataexchange.handler.DataExchange;

public class DataExchangeAPI extends DataExchange {
    /**
     * You should never need to create a custom instance of this Object.
     */
    public DataExchangeAPI() {
        super();
    }

    @Override
    @net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
    protected Connector clientSupplier(DataExchange api) {
        try {
            Class<?> clazz = Class.forName("org.betterx.bclib.api.v2.dataexchange.ConnectorClientside");
            return (Connector) clazz.getDeclaredConstructor(DataExchange.class).newInstance(api);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize client data exchange.", e);
        }
    }

    @Override
    protected ConnectorServerside serverSupplier(DataExchange api) {
        return new ConnectorServerside(api);
    }
}

