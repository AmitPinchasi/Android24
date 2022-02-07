package com.SharxNZ.Utilities;

import com.SharxNZ.Android24;
import com.SharxNZ.Shop.Shop;
import com.SharxNZ.Game.Ability;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

class DoublyNode<T> {

    private T data;

    private DoublyNode<T> next;

    private DoublyNode<T> previous;


    public DoublyNode(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }


    public void setData(T data) {
        this.data = data;
    }


    public DoublyNode<T> getNext() {
        return next;
    }


    public void setNext(DoublyNode<T> next) {
        this.next = next;
    }


    public DoublyNode<T> getPrevious() {
        return previous;
    }


    public void setPrevious(DoublyNode<T> previous) {
        this.previous = previous;
    }

    @Override

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((next == null) ? 0 : next.hashCode());
        result = prime * result + ((previous == null) ? 0 : previous.hashCode());
        return result;
    }

    @Override

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof DoublyNode))
            return false;
        DoublyNode<T> other = (DoublyNode<T>) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (next == null) {
            if (other.next != null)
                return false;
        } else if (!next.equals(other.next))
            return false;
        if (previous == null) {
            if (other.previous != null)
                return false;
        } else if (!previous.equals(other.previous))
            return false;
        return true;
    }

    @Override

    public String toString() {
        return "DoublyNode [data=" + data + "]";
    }

}

public class DoublyCircularLinkedList<T> implements Iterator<T>, Iterable<T> {
    private DoublyNode<T> currentNode;
    private int size = 0;
    private DoublyNode<T> firstNode;
    private int num = 1;

    /**
     * Add element
     *
     * @param data
     */

    public void add(T data) {
        if (data == null) {
            return;
        }

        if (firstNode == null) {
            firstNode = new DoublyNode<>(data);
            firstNode.setNext(firstNode);
            firstNode.setPrevious(firstNode);
            resetIteratorPointer();
        } else {
            DoublyNode<T> newNode = new DoublyNode<>(data);
            DoublyNode<T> lastNode = getLastNode(firstNode);
            lastNode.setNext(newNode);
            // Last node will again connected to first Node;
            newNode.setNext(firstNode);
            newNode.setPrevious(lastNode);
            firstNode.setPrevious(newNode);
        }
        size++;
    }

    /**
     * Delete the first occurrence of element from circular linked list if exists and returns
     * true. If data not available , it returns false;
     *
     * @param data
     * @return
     */

    public boolean delete(T data) {
        if (this.firstNode == null) {
            return false;
        }
        // Delete first element
        if (this.firstNode.getData().equals(data)) {
            DoublyNode<T> lastNode = getLastNode(this.firstNode);
            this.firstNode = this.firstNode.getNext();
            this.firstNode.setPrevious(lastNode);
            lastNode.setNext(this.firstNode);
            resetIteratorPointer();
            size--;
            return true;
        }
        DoublyNode<T> pointerNode = this.firstNode;
        DoublyNode<T> previousNode = null;
        while (pointerNode.getData() != null) {
            if (pointerNode.getData().equals(data)) {
                DoublyNode<T> nextNode = pointerNode.getNext();
                nextNode.setPrevious(previousNode);
                previousNode.setNext(nextNode);
                size--;
                return true;
            } else {
                previousNode = pointerNode;
                pointerNode = pointerNode.getNext();

            }
        }
        return false;
    }

    private DoublyNode<T> getLastNode(DoublyNode<T> node) {

        DoublyNode<T> lastNode = node;
        // Last Node will found if next node equals the passed Node.
        if (!lastNode.getNext().equals(this.firstNode)) {
            return getLastNode(lastNode.getNext());
        } else {
            return lastNode;
        }
    }

    private void fixList(DoublyNode<T> node) {
        if (node.getNext() == null)
            node.setNext(firstNode);
        else
            fixList(node.getNext());
    }

    private DoublyNode<T> next(DoublyNode<T> node) {
        if (node.getNext() != null) {
            return node.getNext();
        } else {
            return null;
        }
    }

    /**
     * counter should be greater than or equal to 1 and less than and equal to size.
     *
     * @param position
     * @return
     */
    private DoublyNode<T> getNode(int position) {
        if (position < 1) {
            return null;
        }
        int counter = 1;
        DoublyNode<T> pointerNode = this.firstNode;
        while (counter <= position) {
            if (counter == position) {
                return pointerNode;
            } else {
                counter++;
                pointerNode = pointerNode.getNext();
            }
        }
        return null;
    }

    public int size() {
        return this.size;
    }

    @Override
    public String toString() {
        if (this.firstNode == null) {
            return "[]";
        }
        String represent = "[" + this.firstNode.toString() + ",";
        DoublyNode<T> nextNode = next(this.firstNode);
        while (nextNode != this.firstNode) {
            represent = represent + nextNode.toString();
            nextNode = next(nextNode);
            if (nextNode != null) {
                represent = represent + ",";
            }
        }
        represent = represent + "]";
        return represent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((firstNode == null) ? 0 : firstNode.hashCode());
        result = prime * result + size;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof DoublyCircularLinkedList))
            return false;
        DoublyCircularLinkedList other = (DoublyCircularLinkedList) obj;
        if (firstNode == null) {
            if (other.firstNode != null)
                return false;
        } else if (!firstNode.equals(other.firstNode))
            return false;
        if (size != other.size)
            return false;
        return true;
    }

    @Override
    public boolean hasNext() {
        if (currentNode == null) {
            if (firstNode != null)
                fixList(firstNode);
            return false;
        } else
            return true;
    }

    public T get() {
        return currentNode.getData();
    }

    @Override
    public T next() {
        T data = currentNode.getData();
        num = num == size ? 1 : num + 1;
        currentNode = currentNode.getNext();
        return data;
    }

    public T nextGet() {
        currentNode = currentNode.getNext();
        num = num == size ? 1 : num + 1;
        return currentNode.getData();
    }

    public T previousGet() {
        currentNode = currentNode.getPrevious();
        num = num == 1 ? size : num - 1;
        return currentNode.getData();
    }

    public boolean isLast() {
        return this.num() == this.size();
    }

    @Override
    public Iterator<T> iterator() {
        if (firstNode != null)
            getLastNode(firstNode).setNext(null);
        return this;
    }

    public MessageEmbed getCurrentEmbed(User user) {
        if (currentNode.getData() instanceof Ability)
            return ((Ability) currentNode.getData()).getEmbed().setFooter(user.getName() + "    num: " + this.num() + "/" + this.size(), user.getAvatarUrl()).build();
        else
            throw new UnsupportedClassVersionError("The supported classes are classes that extends Ability");
    }

    public MessageEmbed startScrollingEvent(User user, String id) {
        scrollingEvent(user, id);
        return getCurrentEmbed(user);
    }

    private void scrollingEvent(User user, String id) {
        Android24.eventWaiter.waitForEvent(ButtonClickEvent.class, bce -> bce.getComponentId().startsWith("sce#")
                && bce.getComponentId().endsWith(id) && bce.getUser().equals(user), bce -> {
            if (bce.getComponentId().split("#")[1].equals("buy")) {
                bce.replyEmbeds(Shop.tryToBuy(((Ability) currentNode.getData()).getName(), user.getIdLong())).setEphemeral(true).queue();
            } else {
                switch (bce.getComponentId().split("#")[1]) {
                    case "right" -> nextGet();
                    case "left" -> previousGet();
                }
                bce.editMessageEmbeds(getCurrentEmbed(user)).queue();
            }
            scrollingEvent(user, id);
        }, 2, TimeUnit.MINUTES, () -> {
        });
    }

    public void resetIteratorPointer() {
        currentNode = this.firstNode;
    }

    public int num() {
        return num;
    }
}
