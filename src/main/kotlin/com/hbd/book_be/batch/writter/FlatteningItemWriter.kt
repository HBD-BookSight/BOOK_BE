package com.hbd.book_be.batch.writter

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter

open class FlatteningItemWriter<T>(
    private val itemWriter: ItemWriter<T>,
) : ItemWriter<List<T>>, StepExecutionListener {

    override fun write(chunk: Chunk<out List<T>>) {
        val flattenedList = mutableListOf<T>()

        for (itemList in chunk) {
            flattenedList.addAll(itemList)
        }

        itemWriter.write(
            Chunk(flattenedList)
        )
    }

    override fun beforeStep(stepExecution: StepExecution) {
        if (itemWriter is StepExecutionListener) {
            return itemWriter.beforeStep(stepExecution)
        }
        super.beforeStep(stepExecution)
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        if (itemWriter is StepExecutionListener) {
            return itemWriter.afterStep(stepExecution)
        }
        return super.afterStep(stepExecution)
    }
}