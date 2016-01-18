package bookshelf.foprender;

import bookshelf.book.BookWriter;

/**
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *
 */
public abstract class AbstractFopElement
{
    abstract void write(BookWriter bookWriter) throws Exception;
}
