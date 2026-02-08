package po.misc.data.text_span




fun List<TextSpan>.copyMutablePairs(): List<MutablePair> = this.map { it.copyMutable() }

infix fun MutableSpan.append(span: TextSpan): Unit = this.append(other =  span)
infix fun MutableSpan.prepend(span: TextSpan): Unit = this.prepend(other =  span)
