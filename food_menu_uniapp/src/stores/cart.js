import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
    // 购物车数据：{ dishId: quantity }
    const cart = ref({})

    // 全局菜品缓存：{ dishId: dishObject }
    // 用于存储所有已加载过的菜品信息，解决切换分类后购物车显示问题
    const dishesCache = ref({})

    // 计算购物车总数量
    const totalCount = computed(() => {
        return Object.values(cart.value).reduce((sum, count) => sum + count, 0)
    })

    // 计算购物车总金额
    const totalPrice = computed(() => {
        let total = 0
        Object.keys(cart.value).forEach(id => {
            const count = cart.value[id]
            if (count > 0) {
                const dish = dishesCache.value[id]
                if (dish) {
                    total += dish.price * count
                }
            }
        })
        return total.toFixed(2)
    })

    // 购物车列表详情
    const cartList = computed(() => {
        return Object.keys(cart.value).map(id => {
            const dish = dishesCache.value[id]
            return dish ? { ...dish, quantity: cart.value[id] } : null
        }).filter(item => item && item.quantity > 0)
    })

    // 获取单个菜品的数量
    const getDishQuantity = (dishId) => {
        return cart.value[dishId] || 0
    }

    // 增加数量
    const addToCart = (dish) => {
        // 确保缓存中有该菜品信息
        if (dish && !dishesCache.value[dish.id]) {
            dishesCache.value[dish.id] = dish
        }

        const id = dish.id || dish.dishId // 兼容不同数据结构
        const currentQty = cart.value[id] || 0
        cart.value[id] = currentQty + 1
        uni.vibrateShort()
    }

    // 减少数量
    const removeFromCart = (dish) => {
        const id = dish.id || dish.dishId
        const currentQty = cart.value[id] || 0
        if (currentQty > 0) {
            cart.value[id] = currentQty - 1
            if (cart.value[id] === 0) {
                delete cart.value[id]
            }
            uni.vibrateShort()
        }
    }

    // 清空购物车
    const clearCart = () => {
        cart.value = {}
    }

    // 批量更新菜品缓存
    const updateDishCache = (dishes) => {
        if (Array.isArray(dishes)) {
            dishes.forEach(dish => {
                if (dish && dish.id) {
                    dishesCache.value[dish.id] = dish
                }
            })
        }
    }

    return {
        cart,
        dishesCache,
        totalCount,
        totalPrice,
        cartList,
        getDishQuantity,
        addToCart,
        removeFromCart,
        clearCart,
        updateDishCache
    }
})
