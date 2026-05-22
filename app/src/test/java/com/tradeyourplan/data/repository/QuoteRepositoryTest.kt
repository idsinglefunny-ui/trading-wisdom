package com.tradeyourplan.data.repository

import com.tradeyourplan.data.local.dao.QuoteDao
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class QuoteRepositoryTest {

    @Mock
    private lateinit var quoteDao: QuoteDao

    private lateinit var repository: QuoteRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = QuoteRepository(quoteDao)
    }

    @Test
    fun `toggleFavorite calls dao toggleFavorite`() = runTest {
        repository.toggleFavorite(1)

        verify(quoteDao).toggleFavorite(1)
    }

    @Test
    fun `deleteQuoteById calls dao deleteQuoteById`() = runTest {
        repository.deleteQuoteById(1)

        verify(quoteDao).deleteQuoteById(1)
    }

    @Test
    fun `getQuoteById calls dao getQuoteById`() = runTest {
        repository.getQuoteById(1)

        verify(quoteDao).getQuoteById(1)
    }
}
