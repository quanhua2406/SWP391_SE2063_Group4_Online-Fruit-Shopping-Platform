package com.mycompany.onlinefruitshopping.dao;

/**
 * Compatibility alias for the original package location.
 *
 * <p>The real implementation lives in
 * {@link com.mycompany.onlinefruitshopping.dao.user.AddressDAO}
 * after the package split. This subclass keeps code that still
 * imports {@code com.mycompany.onlinefruitshopping.dao.AddressDAO}
 * compiling without touching that code.</p>
 */
public class AddressDAO
        extends com.mycompany.onlinefruitshopping.dao.user.AddressDAO {
}
