import { BrowserRouter as RouterProvider } from 'react-router-dom'
import Router from './Router'

function App() {

  return (

    <RouterProvider>
      <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700;800&display=swap" rel="stylesheet" />
      <Router />
    </RouterProvider>
  )
}

export default App