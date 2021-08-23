<template>

<v-data-table
    :headers="headers"
    :items="myPage"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'MyPage',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "cardNumber", value: "cardNumber" },
            { text: "cardBrand", value: "cardBrand" },
            { text: "cardPost", value: "cardPost" },
            { text: "userName", value: "userName" },
            { text: "cardNo", value: "cardNo" },
            { text: "status", value: "status" },
            { text: "orderId", value: "orderId" },
        ],
        myPage : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/mypages')

      this.myPage = temp.data._embedded.mypages;

    },
    methods: {
    }
  }
</script>

