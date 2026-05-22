package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class GetRandomQuoteUseCaseTest {

    @Mock
    private lateinit var repository: QuoteRepository

    private lateinit var useCase: GetRandomQuoteUseCase

    private val testQuote = Quote(
        id = 1,
        content = "Test quote",
        category = Category.DISCIPLINE,
        marketType = MarketType.GENERAL,
        source = QuoteSource.SYSTEM
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GetRandomQuoteUseCase(repository)
    }

    @Test
    fun `invoke returns quote when available`() = runTest {
        `when`(repository.getRandomQuote(null, null, null)).thenReturn(testQuote)

        val result = useCase()

        assertNotNull(result)
    }

    @Test
    fun `invoke returns null when no quotes`() = runTest {
        `when`(repository.getRandomQuote(null, null, null)).thenReturn(null)

        val result = useCase()

        assertNull(result)
    }

    @Test
    fun `invoke filters by category`() = runTest {
        `when`(repository.getRandomQuote(null, Category.DISCIPLINE, null)).thenReturn(testQuote)

        val result = useCase(category = Category.DISCIPLINE)

        assertNotNull(result)
    }
}
